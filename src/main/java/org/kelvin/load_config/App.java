package org.kelvin.load_config;

/**
 * @author <a href="mailto:shasrp@yahoo-inc.com">Shashikiran</a>
 */
public class App
{

    public static void main(String[] args)
    {
        final String filePath = 0 == args.length ? "/Users/shasrp/test.ini" : args[0];
        System.out.println(AppConfig.load(filePath, false, "ubuntu", "production"));
        System.out.println(AppConfig.load(filePath, false, "ubuntu", "production"));
    }
}
