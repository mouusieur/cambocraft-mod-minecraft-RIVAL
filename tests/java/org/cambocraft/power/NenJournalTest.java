package org.cambocraft.power;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public final class NenJournalTest {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static void fails(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); } catch (Throwable e) { if (type.isInstance(e)) return; throw e; }
        throw new AssertionError("Exception attendue : " + type);
    }
    private static PowerCatalog catalog(String suffix) {
        List<PowerCatalog.Entry> entries = new ArrayList<>();
        for (Rarity r : Rarity.values()) entries.add(new PowerCatalog.Entry("cambocraft:" + r.name().toLowerCase(java.util.Locale.ROOT) + suffix, r));
        return new PowerCatalog(entries);
    }
    public static void main(String[] args) {
        PowerCatalog catalog = catalog("");
        fails(IllegalArgumentException.class, () -> new PowerCatalog(List.of()));
        fails(IllegalArgumentException.class, () -> new PowerCatalog.Entry("invalid id", Rarity.RARE));
        var dupe = new ArrayList<PowerCatalog.Entry>();
        for (Rarity r : Rarity.values()) dupe.add(new PowerCatalog.Entry("cambocraft:" + r.name().toLowerCase(java.util.Locale.ROOT), r));
        dupe.add(dupe.get(0));
        fails(IllegalArgumentException.class, () -> new PowerCatalog(dupe));
        fails(UnsupportedOperationException.class, () -> catalog.powersOf(Rarity.RARE).clear());

        var expanded = new ArrayList<PowerCatalog.Entry>();
        for (Rarity r : Rarity.values()) expanded.add(new PowerCatalog.Entry("cambocraft:" + r.name().toLowerCase(java.util.Locale.ROOT), r));
        expanded.add(new PowerCatalog.Entry("cambocraft:autre", Rarity.COMMUN));
        PowerCatalog multiple = new PowerCatalog(expanded);
        check(multiple.powersOf(Rarity.COMMUN).equals(List.of("cambocraft:autre", "cambocraft:commun")), "Ordre stable");
        int[] calls = {0};
        NenState chosen = NenJournal.offer(NenState.initial(), multiple, bound -> calls[0]++ == 0 ? 0 : 1);
        check(chosen.pending().id().equals("cambocraft:commun") && calls[0] == 2, "Choix dans la rareté");
        int[] badCalls = {0};
        fails(IllegalArgumentException.class, () -> NenJournal.offer(NenState.initial(), multiple,
                bound -> badCalls[0]++ == 0 ? 0 : bound));

        NenState initial = NenState.initial();
        IntUnaryOperator zero = bound -> 0;
        NenState offer = NenJournal.offer(initial, catalog, zero);
        check(offer.pending().id().equals("cambocraft:commun"), "Offre commune");
        check(offer.pity().equals(new PityState(1, 1, 1)), "Compteurs avancés");
        check(initial.equals(NenState.initial()), "État initial immuable");
        fails(IllegalStateException.class, () -> NenJournal.offer(offer, catalog, zero));
        fails(IllegalStateException.class, () -> NenJournal.resolve(initial, catalog, true));
        fails(IllegalArgumentException.class, () -> NenJournal.offer(initial, catalog, bound -> bound));
        NenState rejected = NenJournal.resolve(offer, catalog, false);
        check(rejected.pending() == null && rejected.owned().isEmpty() && rejected.pity().equals(offer.pity()), "Rejet sans effacer le pity");
        NenState accepted = NenJournal.resolve(offer, catalog, true);
        check(accepted.owned().get("cambocraft:commun").copies() == 1, "Premier pouvoir");
        NenState secondOffer = NenJournal.offer(accepted, catalog, zero);
        NenState duplicate = NenJournal.resolve(secondOffer, catalog, true);
        check(duplicate.owned().get("cambocraft:commun").copies() == 2, "Doublon");
        check(accepted.owned().get("cambocraft:commun").copies() == 1, "Pas de mutation historique");
        fails(IllegalStateException.class, () -> NenJournal.resolve(secondOffer, catalog("_nouveau"), true));
        check(NenJournal.resolve(secondOffer, catalog("_nouveau"), false).pending() == null, "Rejet après reload");
        NenState equipped = duplicate.equip(List.of("cambocraft:commun"));
        check(equipped.equipped().size() == 1, "Équipement");
        fails(IllegalArgumentException.class, () -> equipped.equip(List.of("cambocraft:absent")));
        fails(IllegalArgumentException.class, () -> equipped.equip(List.of("cambocraft:commun", "cambocraft:commun")));
        fails(IllegalArgumentException.class, () -> new NenState(PityState.INITIAL, Map.of(), List.of("cambocraft:absent"), null));
        fails(UnsupportedOperationException.class, () -> equipped.owned().clear());
        fails(UnsupportedOperationException.class, () -> equipped.equipped().clear());
        var mutableOwned = new java.util.HashMap<String, PowerProgress>();
        for (int i = 0; i < 4; i++) mutableOwned.put("cambocraft:test" + i, PowerProgress.FIRST);
        var mutableSlots = new ArrayList<>(List.of("cambocraft:test0", "cambocraft:test1", "cambocraft:test2"));
        NenState three = new NenState(PityState.INITIAL, mutableOwned, mutableSlots, null);
        mutableOwned.clear();
        mutableSlots.clear();
        check(three.owned().size() == 4 && three.equipped().size() == 3, "Copies défensives");
        fails(IllegalArgumentException.class, () -> three.equip(List.of("cambocraft:test0", "cambocraft:test1", "cambocraft:test2", "cambocraft:test3")));
        PowerProgress p = new PowerProgress(5, 0);
        check(p.level(100, 250) == 3, "Maîtrise requise");
        p = p.gainMastery(100);
        check(p.level(100, 250) == 4 && p.gainMastery(150).level(100, 250) == 5, "Seuils de maîtrise");
        check(p.duplicate().copies() == 5, "Plafond de doublons");
        fails(IllegalArgumentException.class, () -> new PowerProgress(0, 0));
        fails(IllegalArgumentException.class, () -> PowerProgress.FIRST.gainMastery(-1));
        fails(ArithmeticException.class, () -> new PowerProgress(1, Integer.MAX_VALUE).gainMastery(1));
        fails(IllegalArgumentException.class, () -> PowerProgress.FIRST.level(0, 1));
        System.out.println("NenJournalTest : OK");
    }
}
