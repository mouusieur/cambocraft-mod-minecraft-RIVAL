package org.cambocraft.power;

public final class GachaTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void invalid(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Exception attendue");
    }

    public static void main(String[] args) {
        int[] counts = new int[5];
        for (int ticket = 0; ticket < Gacha.TICKET_COUNT; ticket++) counts[Gacha.baseRarity(ticket).ordinal()]++;
        int[] expected = {5500, 2800, 1200, 450, 50};
        check(java.util.Arrays.equals(counts, expected), "Poids des tickets");
        invalid(() -> Gacha.baseRarity(-1));
        invalid(() -> Gacha.baseRarity(10_000));
        invalid(() -> new PityState(-1, 0, 0));
        invalid(() -> new PityState(31, 31, 31));
        invalid(() -> new PityState(0, 81, 81));
        invalid(() -> new PityState(0, 0, 251));
        invalid(() -> new PityState(2, 1, 2));
        invalid(() -> new PityState(0, 2, 1));
        PityState state = PityState.INITIAL;
        for (int i = 0; i < 30; i++) state = Gacha.draw(0, state).pity();
        check(state.equals(new PityState(30, 30, 30)), "30 échecs");
        Gacha.Draw result = Gacha.draw(0, state);
        check(result.rarity() == Rarity.EPIQUE, "Garantie épique au 31e");
        check(result.pity().equals(new PityState(0, 31, 31)), "État après épique");
        check(Gacha.draw(0, new PityState(30, 80, 250)).rarity() == Rarity.MYTHIQUE, "Priorité");
        check(Gacha.draw(0, new PityState(0, 80, 80)).rarity() == Rarity.LEGENDAIRE, "Garantie légendaire");
        check(Gacha.draw(0, new PityState(0, 0, 250)).rarity() == Rarity.MYTHIQUE, "Garantie mythique");
        check(Gacha.draw(9950, new PityState(30, 40, 60)).pity().equals(PityState.INITIAL), "Tirage naturel supérieur");
        check(Gacha.draw(9500, new PityState(0, 79, 240)).pity().equals(new PityState(0, 0, 241)), "Remise à zéro partielle");
        System.out.println("GachaTest : OK");
    }
}
