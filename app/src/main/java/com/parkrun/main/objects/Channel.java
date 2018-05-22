package com.parkrun.main.objects;

import org.json.JSONObject;

public class Channel implements JSONObjectPopulator
{
    private Item item;

    @Override
    public void populate(JSONObject data)
    {
        item = new Item();
        item.populate(data.optJSONObject("item"));
    }

    public Item getItem()
    {
        return item;
    }
}
