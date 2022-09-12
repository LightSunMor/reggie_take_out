import org.springframework.util.AntPathMatcher;

import java.util.Random;

public class Test {
    @org.junit.jupiter.api.Test
    public void test9()
    {
        Random rdm = new Random();
        String hash1 = Integer.toHexString(rdm.nextInt());
        String capstr = hash1.substring(0, 5);
        System.out.println(capstr);
    }
    @org.junit.jupiter.api.Test
    public void test1()
    {
        AntPathMatcher antPathMatcher=new AntPathMatcher();
        System.out.println(antPathMatcher.match("c?m","com"));
        System.out.println(antPathMatcher.match("c?m","cam"));
        System.out.println("---------");
        System.out.println(antPathMatcher.match("*","com"));
        System.out.println("-----------");
        System.out.println (antPathMatcher.match("/*/**","/a/"));
        System.out.println (antPathMatcher.match("/**/*","/a/b"));

    }
}
