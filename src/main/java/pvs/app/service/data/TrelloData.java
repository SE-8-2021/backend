package pvs.app.service.data;

import java.util.ArrayList;

public class TrelloData {
    private final ArrayList<TrelloList> lanes;

    public TrelloData(){
        this.lanes = new ArrayList<>();
    }

    public TrelloList createList(String id, String title, String label, int width) {
        return new TrelloList(id, title, label, width);
    }

    public void addList(TrelloList trelloList) {
        this.lanes.add(trelloList);
    }

    public static class TrelloList {
        private final String id;
        private final String title;
        private final String label;
        private final Width style;
        private final ArrayList<Card> cards;

        public TrelloList(String id, String title, String label, int width) {
            this.id = id;
            this.title = title;
            this.label = label;
            this.style = new Width(width);
            this.cards = new ArrayList<>();
        }

        public void addCard(String id, String title, String label, String description) {
            Card card = new Card(id, title, label, description);
            this.cards.add(card);
        }
    }

    private static class Width {
        private int width = 280;
        public Width(int width) {
            this.width = width;
        }
    }

    private static class Card {
        private final String id;
        private final String title;
        private final String label;
        private final String description;
        public Card(String id, String title, String label, String description) {
            this.id = id;
            this.title = title;
            this.label = label;
            this.description = description;
        }
    }
}
