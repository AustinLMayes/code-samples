package net.avicus.magma.core.redis;

import com.google.gson.JsonObject;

public interface RedisMessage {

  String channel();

  JsonObject write();
}
