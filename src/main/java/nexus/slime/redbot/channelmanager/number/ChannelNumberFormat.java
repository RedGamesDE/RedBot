package nexus.slime.redbot.channelmanager.number;

public enum ChannelNumberFormat {
    DECIMAL("|1|", Integer::toString, c -> {
        try {
            return Integer.parseInt(c);
        } catch (NumberFormatException e) {
            return -1;
        }
    }),
    ROMAN("|I|", RomanNumberFormat::toRoman, RomanNumberFormat::toNumerical);

    private final String identifier;
    private final Serializer serializer;
    private final Deserializer deserializer;

    ChannelNumberFormat(String identifier, Serializer serializer, Deserializer deserializer) {
        this.identifier = identifier;
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public Deserializer getDeserializer() {
        return deserializer;
    }

    public interface Serializer {
        String serialize(int i);
    }

    public interface Deserializer {
        int deserialize(String s);
    }
}
