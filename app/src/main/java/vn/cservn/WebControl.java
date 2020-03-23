package vn.cservn;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

public class WebControl {
    public  static  boolean IsNullEmpty(String s){
        return  s == null || "".equals(s);
    }
    public  static  String toUrlWithsParams(String url, Map<String,String> params)  {
        String j = url.indexOf('?')> 0 ?  "&" : "?";
        String c = "";
        String s = url ;

        for(Map.Entry<String,String> x : params.entrySet())
        {
            if(!"".equals(j))
            {
                s += j;
                j = "";
            }
            s += c + x.getKey() + '=' + x.getValue();
            c = "&";
        }

        return  s;
    }
}
