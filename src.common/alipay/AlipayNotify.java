package alipay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ebank.core.bank.third.Alipay;
import ebank.core.common.util.MD5sign;

/* *
 *类名：AlipayNotify
 *功能：支付宝通知处理类
 *详细：处理支付宝各接口通知返回
 *版本：3.2
 *日期：2011-03-25
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考

 *************************注意*************************
 *调试通知返回时，可查看或改写log日志的写入TXT里的数据，来检查通知返回是否正常
 */
public class AlipayNotify {
	static Logger logger = Logger.getLogger(AlipayNotify.class); 	
    /**
     * HTTPS形式消息验证地址
     */
    private static final String HTTPS_VERIFY_URL = "https://www.alipay.com/cooperate/gateway.do?service=notify_verify&";

    /**
     * HTTP形式消息验证地址
     */
    private static final String HTTP_VERIFY_URL  = "http://notify.alipay.com/trade/notify_query.do?";

    /**
     * 验证消息是否是支付宝发出的合法消息
     * @param params 通知返回来的参数数组
     * @return 验证结果
     */
    public static boolean verify(Map<String, String> params,String pubkey,String partner) {
        String mysign = getMysign(params,pubkey);
        String responseTxt = "true";
        if(params.get("notify_id") != null) {responseTxt = verifyResponse(params.get("notify_id"),partner);}
        String sign = "";
        logger.debug("notify_id:"+params.get("notify_id"));
        logger.debug("responseTxt:"+responseTxt);
        if(params.get("sign") != null) {sign = params.get("sign");}
        if (mysign.equals(sign) && responseTxt.equals("true")) {
            return true;
        } else {
        	 logger.debug("params:"+params+" sign:"+mysign);
            return false;
        }
    }

    /**
     * 根据反馈回来的信息，生成签名结果
     * @param Params 通知返回来的参数数组
     * @return 生成的签名结果
     */
    private static String getMysign(Map<String, String> Params,String pubkey) {
    	
    	MD5sign t = new MD5sign();
		Map<String, String> sPara=t.ParaFilter(Params);
        String mysign = t.BuildMysign(sPara,pubkey,"utf-8");//获得签名结果
        return mysign;
    }

    /**
    * 获取远程服务器ATN结果,验证返回URL
    * @param notify_id 通知校验ID
    * @return 服务器ATN结果
    * 验证结果集：
    * invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空 
    * true 返回正确信息
    * false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
    */
    private static String verifyResponse(String notify_id,String partner) {
        //获取远程服务器ATN结果，验证是否是支付宝服务器发来的请求
        String transport = "http";
        String veryfy_url = "";
        if (transport.equalsIgnoreCase("https")) {
            veryfy_url = HTTPS_VERIFY_URL;
        } else {
            veryfy_url = HTTP_VERIFY_URL;
        }
        veryfy_url = veryfy_url + "partner=" + partner + "&notify_id=" + notify_id;

        return checkUrl(veryfy_url);
    }

    /**
    * 获取远程服务器ATN结果
    * @param urlvalue 指定URL路径地址
    * @return 服务器ATN结果
    * 验证结果集：
    * invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空 
    * true 返回正确信息
    * false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
    */
    private static String checkUrl(String urlvalue) {
        String inputLine = "";

        try {
            URL url = new URL(urlvalue);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection
                .getInputStream()));
            inputLine = in.readLine().toString();
        } catch (Exception e) {
            e.printStackTrace();
            inputLine = "";
        }

        return inputLine;
    }
    
    public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }

}
