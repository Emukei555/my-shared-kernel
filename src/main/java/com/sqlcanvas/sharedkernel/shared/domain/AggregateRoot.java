package com.sqlcanvas.sharedkernel.shared.domain;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 集約ルート (Aggregate Root) の基底クラス。
 * IDの管理と、ドメインイベントの管理を行います。
 *
 * @param <ID> IDの型 (例: ProductionLineId)
 */
@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AggregateRoot<ID> {

    // 集約は必ずIDを持つ
    @Id
    protected ID id;

    // ドメインイベントの一時保管場所
    // @Transient: このフィールドはDBのカラムとして保存しないことを明示
    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    protected AggregateRoot(ID id) {
        this.id = id;
    }

    /**
     * イベントを登録する (例: "生産完了しました")
     * <p>
     * 副作用（メール送信や他集約への伝播）は、このイベントをApplicationService層で
     * 拾って処理することで、ドメインロジックを純粋に保てます。
     * </p>
     */
    protected void registerEvent(Object event) {
        this.domainEvents.add(event);
    }

    /**
     * 溜まったイベントを取り出してクリアする
     * (通常は Repository の save 処理前後や、ApplicationService の最後で呼び出す)
     */
    public List<Object> pullDomainEvents() {
        var events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }
}