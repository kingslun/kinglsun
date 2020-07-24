package org.kings.spring.boot;

/**
 * <p>
 *
 * </p>
 *
 * @author lun.kings
 * @date 2020/7/19 11:47 上午
 * @email lun.kings@zatech.com
 * @since
 */
public class TryCatch {
    public static void main(String[] args) {
        String a = "18n";
        int b = 1;
        int c = 2;
        int d = 3;
        try {
            int e = Integer.parseInt(a);
            b = 10;
            c = 20;
            d = 30;
        } catch (NumberFormatException e) {
            b = 100;
            c = 200;
            d = 300;
            //..
            System.out.println(e.getMessage());
        } finally {
            b = 1000;
            c = 2000;
            d = 3000;
            //close
        }
        System.out.println(String.format("b=%s,c=%s,d=%s", b, c, d));
    }
}
