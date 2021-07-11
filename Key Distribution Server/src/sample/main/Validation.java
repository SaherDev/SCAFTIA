package sample.main;

public class Validation {


    //check user format
    public static boolean validuser (String user) {
      if(user.equals("")  || user.contains("@")){return false;}
       return true;
    }

    //check ip format
    public static boolean validIP (String ip) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {//ipv4
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {//the number ranges is [0-255]
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    //check ip format
    public static boolean validPort (String port) {
        try {
            if ( port == null || port.isEmpty() ) {
                return false;
            }
                int i = Integer.parseInt( port );
                if ( (i < 0) )
                    return false;


            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

}
