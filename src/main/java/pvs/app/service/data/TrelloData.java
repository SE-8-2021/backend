package pvs.app.service.data;

import java.util.ArrayList;

public class TrelloData {
    private final ArrayList<Lane> lanes;

    public TrelloData(){
        this.lanes = new ArrayList<>();
    }

    public Lane createLane(String id, String title, String label, int width) {
        return new Lane(id, title, label, width);
    }

    public void addLane(Lane lane) {
        this.lanes.add(lane);
    }

    public static class Lane {
        private final String id;
        private final String title;
        private final String label;
        private final Width style;
        private final ArrayList<Card> cards;

        public Lane(String id, String title, String label, int width) {
            this.id = id;
            this.title = title;
            this.label = label;
            this.style = new Width(width);
            this.cards = new ArrayList<>();
        }

        public void addCard(Card card) {
            this.cards.add(card);
        }
    }

    private static class Width {
        private int width = 280;
        public Width(int width) {
            this.width = width;
        }
    }

    public static class Card {
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
