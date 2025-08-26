package com.voltriver.demo.etc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EMailValidator {
    /**
     * SMTP 서버의 메시지 스트림에서 PREFIX (결과값) 내용을 읽어온다.
     * 
     * @param in
     * @return
     * @throws IOException
     */
    private static int hear(BufferedReader in) throws IOException {
        String line = null;
        int res = 0;

        while ((line = in.readLine()) != null) {
            String pfx = line.substring(0, 3);
            try {
                res = Integer.parseInt(pfx);
            } catch (Exception ex) {
                res = -1;
            }
            if (line.charAt(3) != '-')
                break;
        }

        return res;
    }

    /**
     * 소켓에 메시지를 보낸다.
     * 
     * @param wr
     * @param text
     * @throws IOException
     */
    private static void say(BufferedWriter wr, String text) throws IOException {
        wr.write(text + "\r\n");
        wr.flush();
    }

    private static ArrayList getMX(String hostName) throws NamingException {
        // 도메인에서 MX레코드를 찾기를 시도
        Hashtable env = new Hashtable();
        env.put("java.naming.factory.initial",
                "com.sun.jndi.dns.DnsContextFactory");
        DirContext ictx = new InitialDirContext(env);
        Attributes attrs = ictx.getAttributes(hostName, new String[] { "MX" });
        Attribute attr = attrs.get("MX");

        // 만약 MX레코드가 없으면, 그 자신 MX서버인지 시도해 본다.
        if ((attr == null) || (attr.size() == 0)) {
            attrs = ictx.getAttributes(hostName, new String[] { "A" });
            attr = attrs.get("A");
            if (attr == null)
                throw new NamingException("호스트명이 잘못되었습니다. '" + hostName + "'");
        }
        // 발견한 경우에 다음을 수행한다.
        ArrayList res = new ArrayList();
        NamingEnumeration en = attr.getAll();

        while (en.hasMore()) {
            String x = (String) en.next();
            String f[] = x.split(" ");
            if (f[1].endsWith("."))
                f[1] = f[1].substring(0, (f[1].length() - 1));
            res.add(f[1]);
        }
        return res;
    }

    /**
     * 메일이 유효한가 여부를 확인하는 메서드
     * 
     * @param address
     * @return
     */
    public static boolean isAddressValid(String address) {
        // 도메인네임 구분자'@'위치를 찾는다.
        int pos = address.indexOf('@');

        // 없다면, 잘못된 e-mail
        if (pos == -1)
            return false;
        // 메일 주소를 찾기위해서 도메인명 만을 구한다.
        String domain = address.substring(++pos);
        ArrayList mxList = null;
        try {
            // DNS에서 MX레코드를 찾는다.
            mxList = getMX(domain);
        } catch (NamingException ex) {
            return false;
        }

        if (mxList.size() == 0)
            return false;
        // 각각의 MX에 SMTP 유효성 체크를 한다.
        for (int mx = 0; mx < mxList.size(); mx++) {
            boolean valid = false;
            Socket skt = null;
            BufferedReader rdr = null;
            BufferedWriter wtr = null;
            try {
                int res;
                skt = new Socket((String) mxList.get(mx), 25);
                rdr = new BufferedReader(new InputStreamReader(skt
                        .getInputStream()));
                wtr = new BufferedWriter(new OutputStreamWriter(skt
                        .getOutputStream()));
                res = hear(rdr);
                if (res != 220) {
                    throw new Exception(String.format("SMTP 메시지 error, res[%s]", res ));
                }
                say(wtr, "EHLO " + domain);
                res = hear(rdr);
                if (res == 500) {
                    System.out.println("HELO로 재시도합니다.");
                    say(wtr, "HELO " + domain);
                    res = hear(rdr);
                    if (res != 250)
                        throw new Exception("ESMTP가 아닙니다.");
                }
                if (res != 250) {
                    throw new Exception("ESMTP가 아닙니다.");
                }
                say(wtr, "MAIL FROM: <" + address + ">");
                res = hear(rdr);
                if (res != 250) {
                    throw new Exception("발송 거부되었습니다.");
                }
                say(wtr, "RCPT TO: <" + address + ">");
                res = hear(rdr);
                say(wtr, "RSET");
                try {
                    hear(rdr);
                } catch (Exception e) {
                }
                say(wtr, "QUIT");
                // hear(rdr); // quit하는 경우 수신을 하지 않아도 무방하다.
                if (res != 250) {
                    throw new Exception("메일주소가 잘못되었습니다. (서버에서 수신자 없음 메시지 리턴)");
                }
                valid = true;
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            } finally {
                if (rdr != null) {
                    try {
                        rdr.close();
                    } catch (IOException e) {
                    }
                }
                if (wtr != null) {
                    try {
                        wtr.close();
                    } catch (IOException e) {
                    }
                }
                if (skt != null) {
                    try {
                        skt.close();
                    } catch (IOException e) {
                    }
                }
            }
            if (valid)
                return true;
        }
        return false;
    }

    public static void main(String args[]) {
        String testData[] = { "neo073@naver.com", "voltriver7@gmail.com" };

        for (int i = 0; i < testData.length; i++) {
            log.info(String.format("testData[%d]=%s, isValid? %s", i, testData[i], isAddressValid(testData[i])));
        }
        return;

    }
}
