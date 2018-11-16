package com.socialassistant_youyuelive.entity;
/**
 * 示例数据
 * {
 "status": true,
 "message": "统一下单接口调用成功",
 "values": {
 "sign": "D5D2FD523B8B6C01412C90F03A7B9099",
 "mch_id": "755437000006",
 "services": "pay.weixin.jspay|pay.weixin.micropay|pay.weixin.native|pay.weixin.app",
 "status": "0",
 "sign_type": "MD5",
 "charset": "UTF-8",
 "nonce_str": "1495765477333851094",
 "token_id": "29a13819627ef80f01ab57605c5d9a5a0",
 "version": "2.0"
 }
 }
 */

public class PayReturnJson{

    /**
     * status : true
     * message : 统一下单接口调用成功
     * values : {"sign":"762455E2E2663016960AA8428BEE47A5","mch_id":"755437000006","services":"pay.weixin.jspay|pay.weixin.micropay|pay.weixin.native|pay.weixin.app","status":"0","sign_type":"MD5","charset":"UTF-8","nonce_str":"1495775283597536786","token_id":"2b093b9495ed1d10caab95ff90597decb","version":"2.0"}
     */

    private boolean status;
    private String message;
    private ValuesBean values;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ValuesBean getValues() {
        return values;
    }

    public void setValues(ValuesBean values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "PayReturnJson{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", values=" + values +
                '}';
    }

    public static class ValuesBean {
        @Override
        public String toString() {
            return "ValuesBean{" +
                    "sign='" + sign + '\'' +
                    ", mch_id='" + mch_id + '\'' +
                    ", services='" + services + '\'' +
                    ", status='" + status + '\'' +
                    ", sign_type='" + sign_type + '\'' +
                    ", charset='" + charset + '\'' +
                    ", nonce_str='" + nonce_str + '\'' +
                    ", token_id='" + token_id + '\'' +
                    ", version='" + version + '\'' +
                    '}';
        }

        /**
         * sign : 762455E2E2663016960AA8428BEE47A5
         * mch_id : 755437000006
         * services : pay.weixin.jspay|pay.weixin.micropay|pay.weixin.native|pay.weixin.app
         * status : 0
         * sign_type : MD5
         * charset : UTF-8
         * nonce_str : 1495775283597536786
         * token_id : 2b093b9495ed1d10caab95ff90597decb
         * version : 2.0
         */

        private String sign;
        private String mch_id;
        private String services;
        private String status;
        private String sign_type;
        private String charset;
        private String nonce_str;
        private String token_id;
        private String version;

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getMch_id() {
            return mch_id;
        }

        public void setMch_id(String mch_id) {
            this.mch_id = mch_id;
        }

        public String getServices() {
            return services;
        }

        public void setServices(String services) {
            this.services = services;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSign_type() {
            return sign_type;
        }

        public void setSign_type(String sign_type) {
            this.sign_type = sign_type;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public String getNonce_str() {
            return nonce_str;
        }

        public void setNonce_str(String nonce_str) {
            this.nonce_str = nonce_str;
        }

        public String getToken_id() {
            return token_id;
        }

        public void setToken_id(String token_id) {
            this.token_id = token_id;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }


}