/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.misc.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.Base64;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Uid;

class AttributeDeserializer extends JsonDeserializer<Attribute> {

    @Override
    public Attribute deserialize(final JsonParser jp, final DeserializationContext ctx)
            throws IOException, JsonProcessingException {

        ObjectNode tree = jp.readValueAsTree();

        String name = tree.get("name").asText();

        List<Object> values = new ArrayList<Object>();
        for (Iterator<JsonNode> itor = tree.get("value").iterator(); itor.hasNext();) {
            JsonNode node = itor.next();
            if (node.isNull()) {
                values.add(null);
            } else if (node.isObject()) {
                values.add(((ObjectNode) node).traverse(jp.getCodec()).readValueAs(GuardedString.class));
            } else if (node.isBoolean()) {
                values.add(node.asBoolean());
            } else if (node.isDouble()) {
                values.add(node.asDouble());
            } else if (node.isLong()) {
                values.add(node.asLong());
            } else if (node.isInt()) {
                values.add(node.asInt());
            } else {
                String text = node.asText();
                if (text.startsWith(AttributeSerializer.BYTE_ARRAY_PREFIX)
                        && text.endsWith(AttributeSerializer.BYTE_ARRAY_SUFFIX)) {

                    values.add(Base64.decode(StringUtils.substringBetween(
                            text, AttributeSerializer.BYTE_ARRAY_PREFIX, AttributeSerializer.BYTE_ARRAY_SUFFIX)));
                } else {
                    values.add(text);
                }
            }
        }

        return Uid.NAME.equals(name)
                ? new Uid(values.isEmpty() || values.get(0) == null ? null : values.get(0).toString())
                : Name.NAME.equals(name)
                ? new Name(values.isEmpty() || values.get(0) == null ? null : values.get(0).toString())
                : AttributeBuilder.build(name, values);
    }

}
