package com.fresconews.fresco.framework.network;

import com.fresconews.fresco.framework.network.responses.NetworkFrescoObject;
import com.fresconews.fresco.framework.network.responses.NetworkGallery;
import com.fresconews.fresco.framework.network.responses.NetworkStory;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FrescoMixedListDeserializer implements JsonDeserializer<List<NetworkFrescoObject>> {
    @Override
    public List<NetworkFrescoObject> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<NetworkFrescoObject> result = new ArrayList<>(json.getAsJsonArray().size());
        for (JsonElement object : json.getAsJsonArray()) {
            if (object.getAsJsonObject().get("object").getAsString().equals("story")) {
                NetworkStory story = context.deserialize(object, NetworkStory.class);
                result.add(story);
            }
            else {
                NetworkGallery gallery = context.deserialize(object, NetworkGallery.class);
                result.add(gallery);
            }
        }
        return result;
    }
}
