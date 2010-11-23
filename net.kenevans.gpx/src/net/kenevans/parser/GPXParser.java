package net.kenevans.parser;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import net.kenevans.gpx.BoundsType;
import net.kenevans.gpx.EmailType;
import net.kenevans.gpx.GpxType;
import net.kenevans.gpx.MetadataType;
import net.kenevans.gpx.PersonType;
import net.kenevans.gpx.RteType;
import net.kenevans.gpx.TrkType;
import net.kenevans.gpx.TrksegType;
import net.kenevans.gpx.WptType;
import net.kenevans.gpx10.Gpx;
import net.kenevans.gpx10.Gpx.Rte;
import net.kenevans.gpx10.Gpx.Rte.Rtept;
import net.kenevans.gpx10.Gpx.Trk;
import net.kenevans.gpx10.Gpx.Trk.Trkseg;
import net.kenevans.gpx10.Gpx.Trk.Trkseg.Trkpt;
import net.kenevans.gpx10.Gpx.Wpt;

/*
 * Created on Aug 19, 2010
 * By Kenneth Evans, Jr.
 */

public class GPXParser
{
    /** Hard-coded file name for testing with the main method. */
    private static String TEST_FILE = "C:/Users/evans/Documents/GPSLink/CM2008.gpx";
    /** This is the package specified when XJC was run. */
    private static String GPX_11_PACKAGE = "net.kenevans.gpx";
    /** This is the GPX 1.0 package. */
    private static String GPX_10_PACKAGE = "net.kenevans.gpx10";

    /**
     * Save a GpxType object into a file with the given name.
     * 
     * @param gpx
     * @param fileName
     * @throws JAXBException
     */
    public static void save(String creator, GpxType gpx, String fileName)
        throws JAXBException {
        save(creator, gpx, new File(fileName));
    }

    /**
     * Save a GpxType object into a File.
     * 
     * @param gpx
     * @param file
     * @throws JAXBException
     */
    public static void save(String creator, GpxType gpx, File file)
        throws JAXBException {
        // Set the creator
        if(creator != null) {
            gpx.setCreator(creator);
        }
        // Reset the version
        gpx.setVersion("1.1");

        // Create a new JAXBElement<GpxType> for the marshaller
        QName qName = new QName("http://www.topografix.com/GPX/1/1", "gpx");
        JAXBElement<GpxType> root = new JAXBElement<GpxType>(qName,
            GpxType.class, gpx);
        // Create a context
        JAXBContext jc = JAXBContext.newInstance(GPX_11_PACKAGE);
        // Create a marshaller
        Marshaller marshaller = jc.createMarshaller();
        // Set it to be formatted, otherwise it is one long line
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        // Marshal
        marshaller.marshal(root, file);
    }

    /**
     * Parses a GPX file with the given name.
     * 
     * @param fileName The file name to parse.
     * @return The GpxType corresponding to the top level of the input file.
     * @throws JAXBException
     */
    public static GpxType parse(String fileName) throws JAXBException {
        return parse(new File(fileName));
    }

    /**
     * Parses a GPX file. Tries to open as GPX 1.1. On failure with an
     * indication of its being a 1.0 file, tries to open s 1.0 and convert.
     * 
     * @param file The File to parse.
     * @return The GpxType corresponding to the top level of the input file.
     * @throws JAXBException
     */
    @SuppressWarnings("unchecked")
    public static GpxType parse(File file) throws JAXBException {
        GpxType gpx = null;
        JAXBContext jc = JAXBContext.newInstance(GPX_11_PACKAGE);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        try {
            JAXBElement<GpxType> root = (JAXBElement<GpxType>)unmarshaller
                .unmarshal(file);
            gpx = root.getValue();
        } catch(JAXBException ex) {
            if(ex.getMessage() != null
                && ex.getMessage()
                    .contains("http://www.topografix.com/GPX/1/0")) {
                // Is a GPX 1.0 file
                Gpx gpx10 = parse10(file);
                if(gpx10 != null) {
                    gpx = convertGpx10toGpx11(gpx10);
                }
            } else {
                // Some other problem, rethrow the exception
                throw (ex);
            }
        }
        return gpx;
    }

    /**
     * Parses a GPX 1.0 file.
     * 
     * @param file The File to parse.
     * @return The Gpx corresponding to the top level of the input file.
     * @throws JAXBException
     */
    public static Gpx parse10(File file) throws JAXBException {
        Gpx gpx = null;
        JAXBContext jc = JAXBContext.newInstance(GPX_10_PACKAGE);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Object obj = unmarshaller.unmarshal(file);
        if(obj != null) {
            gpx = (Gpx)obj;
        }
        return gpx;
    }

    /**
     * Converts a GPX 1.0 Gpx type to a GPX 1.1 GpxType type by copying common
     * fields. Note that this implementation may not be complete and may not
     * convert everything that could be converted.
     * 
     * @param gpx10 The GPX 1.0 type to convert.
     * @return The GpxType.
     */
    public static GpxType convertGpx10toGpx11(Gpx gpx10) {
        GpxType gpx = new GpxType();
        String stringVal = gpx10.getCreator();
        if(stringVal != null) {
            gpx.setCreator(stringVal);
        }
        stringVal = gpx10.getVersion();
        if(stringVal != null) {
            gpx.setVersion(stringVal);
        }

        // Metadata
        MetadataType metadataType = new MetadataType();
        boolean doMetadata = false;
        // Author
        boolean doPerson = false;
        PersonType personType = new PersonType();
        String email = gpx10.getEmail();
        EmailType emailType = null;
        if(emailType != null) {
            doPerson = true;
            emailType = new EmailType();
            int index = email.indexOf("@");
            emailType.setId(email.substring(0, index));
            emailType.setDomain(email.substring(index + 1));
            personType.setEmail(emailType);
        }
        // Link (not implemented, no person link in GPX 1.0)
        // personType.setLink(value);
        // Name (not implemented, no person name in GPX 1.0)
        // personType.setName(value);
        if(doPerson) {
            doMetadata = true;
            metadataType.setAuthor(personType);
        }
        // Bounds
        boolean doBounds = false;
        BoundsType boundsType = new BoundsType();
        net.kenevans.gpx10.BoundsType boundsType10 = gpx10.getBounds();
        if(boundsType10 != null) {
            BigDecimal val = boundsType10.getMaxlat();
            if(val != null) {
                doBounds = true;
                boundsType.setMaxlat(val);
            }
            val = boundsType10.getMaxlon();
            if(val != null) {
                doBounds = true;
                boundsType.setMaxlon(val);
            }
            val = boundsType10.getMinlat();
            if(val != null) {
                doBounds = true;
                boundsType.setMinlat(val);
            }
            val = boundsType10.getMinlon();
            if(val != null) {
                doBounds = true;
                boundsType.setMinlon(val);
            }
            if(doBounds) {
                doMetadata = true;
                metadataType.setBounds(boundsType);
            }
        }
        // Copyright (not implemented, no copyright in GPX 1.0)
        stringVal = gpx10.getDesc();
        if(stringVal != null) {
            doMetadata = true;
            metadataType.setDesc(stringVal);
        }
        stringVal = gpx10.getName();
        if(stringVal != null) {
            doMetadata = true;
            metadataType.setName(stringVal);
        }
        stringVal = gpx10.getKeywords();
        if(stringVal != null) {
            doMetadata = true;
            metadataType.setKeywords(stringVal);
        }
        metadataType.setTime(gpx10.getTime());
        XMLGregorianCalendar timeVal = gpx10.getTime();
        if(timeVal != null) {
            doMetadata = true;
            metadataType.setTime(timeVal);
        }
        if(doMetadata) {
            gpx.setMetadata(metadataType);
        }

        // Waypoints
        List<Wpt> wpts10 = gpx10.getWpt();
        if(wpts10 != null && wpts10.size() > 0) {
            WptType wptType = null;
            // Loop over tracks
            for(Wpt wpt : wpts10) {
                if(wpt == null) {
                    continue;
                }
                wptType = new WptType();
                if(wptType == null) {
                    continue;
                }
                gpx.getWpt().add(wptType);
                wptType.setAgeofdgpsdata(wpt.getAgeofdgpsdata());
                wptType.setCmt(wpt.getCmt());
                wptType.setDesc(wpt.getDesc());
                wptType.setDgpsid(wpt.getDgpsid());
                wptType.setEle(wpt.getEle());
                wptType.setFix(wpt.getFix());
                wptType.setGeoidheight(wpt.getGeoidheight());
                wptType.setLat(wpt.getLat());
                wptType.setLon(wpt.getLon());
                wptType.setMagvar(wpt.getMagvar());
                wptType.setName(wpt.getName());
                wptType.setPdop(wpt.getPdop());
                wptType.setSat(wpt.getSat());
                wptType.setSrc(wpt.getSrc());
                wptType.setSym(wpt.getSym());
                wptType.setTime(wpt.getTime());
                wptType.setType(wpt.getType());
                wptType.setVdop(wpt.getVdop());
                wptType.setCmt(wpt.getCmt());
                wptType.setDesc(wpt.getDesc());
                wptType.setName(wpt.getName());
            }
        }

        // Tracks
        List<Trk> trks10 = gpx10.getTrk();
        if(trks10 != null && trks10.size() > 0) {
            TrkType trkType = null;
            TrksegType trksegType = null;
            WptType wptType = null;
            List<Trkseg> trkSegs = null;
            List<Trkpt> trkpts = null;
            // Loop over tracks
            for(Trk trk : trks10) {
                if(trk == null) {
                    continue;
                }
                trkType = new TrkType();
                if(trkType == null) {
                    continue;
                }
                gpx.getTrk().add(trkType);
                trkType.setCmt(trk.getCmt());
                trkType.setDesc(trk.getDesc());
                trkType.setName(trk.getName());
                trkType.setNumber(trk.getNumber());
                trkType.setSrc(trk.getSrc());
                trkSegs = trk.getTrkseg();
                // Loop over track segments
                for(Trkseg trkseg : trkSegs) {
                    if(trkseg == null) {
                        continue;
                    }
                    trksegType = new TrksegType();
                    if(trksegType == null) {
                        continue;
                    }
                    trkType.getTrkseg().add(trksegType);
                    trkpts = trkseg.getTrkpt();
                    // Loop over track points
                    for(Trkpt trkpt : trkpts) {
                        if(trkpt == null) {
                            continue;
                        }
                        wptType = new WptType();
                        if(wptType == null) {
                            continue;
                        }
                        trksegType.getTrkpt().add(wptType);
                        wptType.setAgeofdgpsdata(trkpt.getAgeofdgpsdata());
                        wptType.setCmt(trkpt.getCmt());
                        wptType.setDesc(trkpt.getDesc());
                        wptType.setDgpsid(trkpt.getDgpsid());
                        wptType.setEle(trkpt.getEle());
                        wptType.setFix(trkpt.getFix());
                        wptType.setGeoidheight(trkpt.getGeoidheight());
                        wptType.setLat(trkpt.getLat());
                        wptType.setLon(trkpt.getLon());
                        wptType.setMagvar(trkpt.getMagvar());
                        wptType.setName(trkpt.getName());
                        wptType.setPdop(trkpt.getPdop());
                        wptType.setSat(trkpt.getSat());
                        wptType.setSrc(trkpt.getSrc());
                        wptType.setSym(trkpt.getSym());
                        wptType.setTime(trkpt.getTime());
                        wptType.setType(trkpt.getType());
                        wptType.setVdop(trkpt.getVdop());
                    }
                }
            }
        }

        // Routes
        List<Rte> rtes10 = gpx10.getRte();
        if(rtes10 != null && rtes10.size() > 0) {
            RteType rteType = null;
            WptType wptType = null;
            List<Rtept> rtepoints;
            // Loop over tracks
            for(Rte rte : rtes10) {
                if(rte == null) {
                    continue;
                }
                rteType = new RteType();
                if(rteType == null) {
                    continue;
                }
                gpx.getRte().add(rteType);
                rteType.setCmt(rte.getCmt());
                rteType.setDesc(rte.getDesc());
                rteType.setName(rte.getName());
                rteType.setNumber(rte.getNumber());
                rteType.setSrc(rte.getSrc());
                rtepoints = rte.getRtept();
                // Loop over route points
                for(Rtept rtept : rtepoints) {
                    if(rtept == null) {
                        continue;
                    }
                    wptType = new WptType();
                    if(wptType == null) {
                        continue;
                    }
                    rteType.getRtept().add(wptType);
                    wptType.setAgeofdgpsdata(rtept.getAgeofdgpsdata());
                    wptType.setCmt(rtept.getCmt());
                    wptType.setDesc(rtept.getDesc());
                    wptType.setDgpsid(rtept.getDgpsid());
                    wptType.setEle(rtept.getEle());
                    wptType.setFix(rtept.getFix());
                    wptType.setGeoidheight(rtept.getGeoidheight());
                    wptType.setLat(rtept.getLat());
                    wptType.setLon(rtept.getLon());
                    wptType.setMagvar(rtept.getMagvar());
                    wptType.setName(rtept.getName());
                    wptType.setPdop(rtept.getPdop());
                    wptType.setSat(rtept.getSat());
                    wptType.setSrc(rtept.getSrc());
                    wptType.setSym(rtept.getSym());
                    wptType.setTime(rtept.getTime());
                    wptType.setType(rtept.getType());
                    wptType.setVdop(rtept.getVdop());
                }
            }
        }

        return gpx;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String fileName = TEST_FILE;
        System.out.println(fileName);
        GpxType gpx = null;
        try {
            gpx = parse(fileName);
        } catch(JAXBException ex) {
            System.out
                .println("Error creating JAXBContext: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        List<TrkType> tracks = gpx.getTrk();
        for(TrkType track : tracks) {
            System.out.println(track.getName());
        }
    }

}
