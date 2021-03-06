/*
 * Copyright (c) 2015 Guillaume Hillairet.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Guillaume Hillairet - initial API and implementation
 *
 */
package org.emfjson.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.emfjson.jackson.databind.EMFContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionDeserializer extends JsonDeserializer<Collection<Object>> {

	private final JsonDeserializer<? extends EObject> deserializer;
	private final JsonDeserializer<? extends ReferenceEntry> referenceDeserializer;

	public CollectionDeserializer(
			JsonDeserializer<? extends EObject> deserializer,
			JsonDeserializer<ReferenceEntry> referenceDeserializer) {
		this.deserializer = deserializer;
		this.referenceDeserializer = referenceDeserializer;
	}

	@Override
	public Collection<Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		List<Object> values = new ArrayList<>();

		while (p.nextToken() != JsonToken.END_ARRAY) {
			EObject result = deserializer.deserialize(p, ctxt);
			if (result != null) {
				values.add(result);
			}
		}
		return values;
	}

	@Override
	public Collection<Object> deserialize(JsonParser p, DeserializationContext ctxt, Collection<Object> intoValue) throws IOException {
		final EObject parent = EMFContext.getParent(ctxt);
		final EReference feature = EMFContext.getReference(ctxt);

		while (p.nextToken() != JsonToken.END_ARRAY) {
			EMFContext.setParent(ctxt, parent);
			EMFContext.setFeature(ctxt, feature);

			if (feature != null && feature.isContainment()) {
				EObject result = deserializer.deserialize(p, ctxt);
				if (result != null) {
					intoValue.add(result);
				}
			} else {
				ReferenceEntry entry = referenceDeserializer.deserialize(p, ctxt);
				if (entry != null) {
					intoValue.add(entry);
				}
			}
		}
		return intoValue;
	}
}
