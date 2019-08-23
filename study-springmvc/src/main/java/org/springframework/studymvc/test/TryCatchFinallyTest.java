package org.springframework.studymvc.test;

/**
 * @author 周宁
 * @Date 2019-08-20 16:24
 */
public class TryCatchFinallyTest {

    public static void main(String[] args) {
        sixs(1);
    }

    private static void sixs(int i){
        try {
            if(i==1){
                return;
            }
        }finally {
            System.out.println("always exeute");
        }
    }
}
