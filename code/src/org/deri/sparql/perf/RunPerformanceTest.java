package org.deri.sparql.perf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.deri.sparql.perf.test.PerformanceTest;

public class RunPerformanceTest
{
    static Logger _log = Logger.getLogger(RunPerformanceTest.class.getName());

    private static final long POLITE_WAIT = 1 * 1000;

    public static void main(String[] args) throws IOException
    {
        Options options = new Options();

        Option testsOpt = new Option("s",
                "setup files for tests (or directory), can have multiple");
        testsOpt.setRequired(true);
        testsOpt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(testsOpt);

        Option epOpt = new Option("e",
                "file containing list of endpoints to test");
        epOpt.setRequired(true);
        epOpt.setArgs(1);
        options.addOption(epOpt);

        Option outOpt = new Option("o",
                "output dir for results (stats printed to standard out!)");
        outOpt.setRequired(true);
        outOpt.setArgs(1);
        options.addOption(outOpt);

        // Option threadsOpt = new Option("t",
        // "threads for pool (default single-threaded)");
        // threadsOpt.setArgs(1);
        // options.addOption(threadsOpt);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;

        try
        {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e)
        {
            System.err.println("***ERROR: " + e.getClass() + ": "
                    + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("parameters:", options);
            return;
        }

        if (cmd.hasOption("h"))
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("parameters:", options);
            return;
        }

        ArrayList<String> endpoints = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(
                cmd.getOptionValue(epOpt.getOpt())));
        _log.info("Reading endpoints from " + epOpt.getOpt() + " ...");
        String line = null;
        while ((line = br.readLine()) != null)
        {
            line = line.trim();
            if (!line.isEmpty())
            {
                endpoints.add(line);
            }
        }
        br.close();
        _log.info("... found " + endpoints.size() + " endpoints.");

        ArrayList<String[]> tests = new ArrayList<String[]>();
        for (String f : cmd.getOptionValues(testsOpt.getOpt()))
        {
            _log.info("Opening test from " + f + " ...");
            File file = new File(f);
            if (file.isDirectory())
            {
                File[] dir = file.listFiles();
                for (File f2 : dir)
                {
                    br = new BufferedReader(new FileReader(f2));
                    StringBuffer buf = new StringBuffer();
                    while ((line = br.readLine()) != null)
                    {
                        buf.append(line + "\n");
                    }
                    String query = buf.toString();
                    tests.add(new String[] { f2.getName(), query });
                    _log.info("... loaded query: "
                            + Utils.removeNewlines(query));
                }
            }
            else
            {
                br = new BufferedReader(new FileReader(f));
                StringBuffer buf = new StringBuffer();
                while ((line = br.readLine()) != null)
                {
                    buf.append(line + "\n");
                }
                String query = buf.toString();
                tests.add(new String[] { file.getName(), query });
                _log.info("... loaded query: " + Utils.removeNewlines(query));
            }
        }

        String outdir = cmd.getOptionValue(outOpt.getOpt()) + "/";
        new File(outdir).mkdirs();

        for (String[] test : tests)
        {
            _log.info("Running test " + test[0] + " ...");
            String dir = outdir + test[0] + "-results/";
            new File(dir).mkdirs();
            int e = 0;
            for (String ep : endpoints)
            {
                e++;
                _log.info("... for " + ep + " ... (" + e + " of "
                        + endpoints.size() + ")");
                String filename = dir + URLEncoder.encode(ep, "UTF-8");
                PerformanceTest pt = new PerformanceTest(test[0], ep, test[1],
                        filename);
                pt.execute();
                pt.close();

                // wait politely
                try
                {
                    Thread.sleep(POLITE_WAIT);
                }
                catch (InterruptedException ie)
                {
                    ;
                }
            }
            _log.info("... test " + test[0] + " finished.");
        }
        _log.info("All tests have been run!");
    }
}
