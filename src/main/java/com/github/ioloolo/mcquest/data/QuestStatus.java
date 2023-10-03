package com.github.ioloolo.mcquest.data;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.github.ioloolo.mcquest.util.db.Data;
import com.google.cloud.firestore.DocumentReference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class QuestStatus extends Data {

	DocumentReference nowQuest;
	int money;
	List<DocumentReference> completedQuests;
	int huntCount;

	@SneakyThrows({InterruptedException.class, ExecutionException.class})
	public Quest getNowQuestOriginal() {
		if (nowQuest == null) return null;

		Quest quest = nowQuest.get().get().toObject(Quest.class);

		assert quest != null;
		quest.setId(UUID.fromString(nowQuest.getId()));

		return quest;
	}
}
