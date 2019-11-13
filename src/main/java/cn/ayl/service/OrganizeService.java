package cn.ayl.service;

import cn.ayl.intf.Organize;
import cn.ayl.util.json.JsonObject;

public class OrganizeService implements Organize {

    @Override
    public JsonObject login(String name, Integer pwd, Boolean isRole) {
        return JsonObject.Success().append("name", name).append("pwd", pwd).append("isRole", isRole);
    }

}