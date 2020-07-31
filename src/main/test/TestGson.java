import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
//Gson不但可以和map建立关系

//也可以和对象建立关系
class Hero{
    public String name;
    public String skill1;
    public String skill2;
    public String skill3;
    public String skill4;
}

public class TestGson {
    public static void main(String[] args) {
        /*
        HashMap<String,Object> hashMap=new HashMap<String, Object>();
        hashMap.put("name","曹操");
        hashMap.put("skill1","剑气");
        hashMap.put("skill2","三段跳");
        hashMap.put("skill3","加攻击并吸血");
        hashMap.put("skill4","加攻速");
        */

        Hero hero=new Hero();
        hero.name="曹操";
        hero.skill1="剑气";
        hero.skill2="三段跳";
        hero.skill3="加攻击并吸血";
        hero.skill4="加攻速";

        //通过map转成JSON结构的字符串
        //1、创建一个gson对象(这里是通过一个工厂类去创建的该对象)
        Gson gson=new GsonBuilder().create();
        //2、使用 toJson 方法把键值对结构转成 JSON 字符串
        //String str=gson.toJson(hashMap);//这里就可以看出gson和map能建立关系

        String str=gson.toJson(hero);//这里就可以看出gson和对象能建立关系
        System.out.println(str);

    }
}
