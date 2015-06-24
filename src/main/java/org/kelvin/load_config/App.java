package org.kelvin.load_config;

/**
 * @author <a href="mailto:shasrp@yahoo-inc.com">Shashikiran</a>
 */
public class App
{

    public static void main(String[] args)
    {
        final String filePath = 0 == args.length ? "/Users/shasrp/test.ini" : args[0];
        AppConfig config = AppConfig.load(filePath, false, "", "ubuntu", "production", "final");
        System.out.println(config.get("one.f4"));
        System.out.println(config);
    }
}
