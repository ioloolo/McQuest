package com.github.ioloolo.mcquest.data;

import java.util.Map;

import com.github.ioloolo.mcquest.util.bridge.SimpleLocation;
import com.github.ioloolo.mcquest.util.db.Data;
import com.github.ioloolo.mcquest.util.db.Storage;
import com.google.cloud.firestore.DocumentReference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class Quest extends Data {

	String name;
	String description;
	Type type;
	int reward;
	SimpleLocation location;
	Map<String, Object> data;

	public DocumentReference toReference() {
		return Storage.of(Quest.class).reference(this.getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		Quest quest = (Quest)o;

		if (!name.equals(quest.name)) {
			return false;
		}
		return type == quest.type;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}

	@Getter
	@RequiredArgsConstructor
	public enum Type {
		VISIT("방문"),
		HUNT("사냥"),
		;

		private final String name;
	}

	public enum Status {
		NOT_STARTED,
		STARTED,
		COMPLETED,
		;
	}
}
