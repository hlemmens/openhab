package org.openhab.binding.ebus.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openhab.binding.ebus.internal.configuration.TelegramConfiguration;
import org.openhab.binding.ebus.internal.configuration.TelegramValue;

public class EBusEbusdCsvReader {

    public static void main(String[] args) {
        try {
            new EBusEbusdCsvReader().run(args[0], args[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void run(String filepath, String jsonFilePath) throws IOException {

        final Reader in = new FileReader(filepath);

        HashMap<String, TelegramConfiguration> x = new HashMap<String, TelegramConfiguration>();
        ArrayList<TelegramConfiguration> list = new ArrayList<TelegramConfiguration>();

        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
        for (CSVRecord record : records) {

            String type0 = record.get(0);
            String circuit = record.get(1);
            String name = record.get(2);
            String comment = record.get(3);
            String qq = record.get(4);
            String zz = record.get(5);
            String pbsb = record.get(6);
            String id = record.get(7);
            String field1 = record.get(8);
            String partms = record.get(9);
            String datatype = record.get(10);

            String divider = record.get(11);
            String unit = record.get(12);
            String commentx = record.get(13);

            if (type0.startsWith("#") && !type0.startsWith("#,")) {
                // skip comment lines
                continue;
            }

            for (String type : type0.split(Pattern.quote(";"))) {

                if (type.equals("#")) {
                    type = "*ROOT";
                    // file info
                    // #,BAI00,ecoTEC,308523 174,,,,,,,,,,
                }

                TelegramConfiguration parentCfg = x.get(type.startsWith("*") ? "ROOT" : type);
                TelegramConfiguration cfg = new TelegramConfiguration();

                if (parentCfg != null) {
                    cfg.setComment(StringUtils.defaultIfEmpty(parentCfg.getComment(), comment));
                    cfg.setClazz(StringUtils.defaultIfEmpty(parentCfg.getClazz(), circuit));
                    cfg.setCommand(StringUtils.defaultIfEmpty(parentCfg.getCommand(), pbsb));
                    cfg.setDst(StringUtils.defaultIfEmpty(parentCfg.getDst(), zz));
                    cfg.setData(StringUtils.defaultIfEmpty(parentCfg.getData(), "") + "" + id);

                } else {
                    cfg.setComment(comment);
                    cfg.setClazz(circuit);
                    cfg.setCommand(pbsb);
                    cfg.setDst(zz);
                    cfg.setData(id);
                }

                // no inherit
                cfg.setId(name);

                // fake template.csv
                HashMap<String, TelegramValue> values = new HashMap<String, TelegramValue>();

                TelegramValue t = new TelegramValue();
                t.setLabel(commentx);

                t.setPos(12);
                if (type.equals("w")) {
                    t.setPos(9);
                }

                final String d1cTypes[] = { "D1C", "percent1", "temp1", "hours1", "calibration1" };
                final String d2bTypes[] = { "D2B", "temp2", "press", "percent2" };
                final String d2cTypes[] = { "D2C", "temp", "percent", "calibration", "tempsensor" };

                final String ucharTypes[] = { "UCH", "onoff", "temp0", "unit", "fueltype", "countertype",
                        "opdatacyclic", "opdatatransfer", "opdataheat", "opdataaction", "desiredtemp", "percent0",
                        "daysel", "days", "minutes0", "hours", "hour", "seconds0", "calibration0", "mcmode", "mctype",
                        "hwcmode", "status", "yesno", "errorok", "sensor", "backtype", "backmode", "power", "pumppow",
                        "pumpstate", "dcfstate", "phaseok", "switchvalve", "hpstatus" };

                final String charTypes[] = { "SCH", "outsidetemp", "percents", "temps", "calibrations" };

                final String intTypes[] = { "SIN", "temps2", "integral" };
                final String uintTypes[] = { "UIN", "minutes2", "hoursum2", "seconds2", "uin100", "curve", "energy",
                        "cntstarts2", "frequency" };

                if (ArrayUtils.contains(d1cTypes, datatype)) {
                    t.setType("data1c");
                    values.put(name, t);

                } else if (ArrayUtils.contains(d2bTypes, datatype)) {
                    t.setType("data2b");
                    values.put(name, t);

                } else if (ArrayUtils.contains(d2cTypes, datatype)) {
                    t.setType("data2c");
                    values.put(name, t);

                } else if (ArrayUtils.contains(ucharTypes, datatype)) {
                    t.setType("uchar");
                    values.put(name, t);

                } else if (ArrayUtils.contains(charTypes, datatype)) {
                    t.setType("char");
                    values.put(name, t);

                } else if (ArrayUtils.contains(uintTypes, datatype)) {
                    t.setType("uint");
                    values.put(name, t);

                } else if (ArrayUtils.contains(intTypes, datatype)) {
                    t.setType("int");
                    values.put(name, t);

                } else if (datatype.equals("cntminutes")) {
                    datatype = "BCD";

                } else if (datatype.equals("cnthours") || datatype.equals("cnthours")) {
                    datatype = "BCD:3";

                } else if (datatype.equals("cntfuel")) {
                    datatype = "BCD:4";

                }

                if (datatype.startsWith("BCD")) {
                    String substring = datatype.substring(datatype.indexOf(':'));
                    System.out.println("EBusEbusdCsvReader.run()");
                }

                if (datatype.equals("calibrations") || datatype.equals("seconds")) {
                    t.setFactor(new BigDecimal("0.5"));
                }

                if (datatype.equals("curve") || datatype.equals("uin100")) {
                    t.setFactor(new BigDecimal("0.01"));
                }

                if (datatype.equals("minutes")) {
                    t.setFactor(new BigDecimal("0.0083333333333333"));
                }

                if (datatype.equals("tempsensor")) {
                    TelegramValue t2 = new TelegramValue();
                    t2.setPos(t.getPos() + 2);
                    t2.setType("uchar");
                    t2.setLabel("Status " + t.getLabel());
                    values.put("status", t2);
                }

                if (type.startsWith("*")) {
                    cfg.setValues(values);
                    x.put(type.substring(1), cfg);

                } else {
                    if (!values.isEmpty()) {
                        cfg.setValues(values);
                        list.add(cfg);
                        System.out.println("[  OK  ] " + name);
                    } else {
                        System.err.println("[FAILED] " + name);
                    }

                }

            }
        }

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(jsonFilePath), list);

        System.out.println(list.size());
    }
}
