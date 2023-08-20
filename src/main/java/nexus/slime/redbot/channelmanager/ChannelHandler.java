package nexus.slime.redbot.channelmanager;

import nexus.slime.redbot.channelmanager.number.ChannelNumberFormat;

import java.io.IOError;
import java.io.IOException;
import java.util.regex.Pattern;

public class ChannelHandler {
    private String prefix;
    private String suffix;
    private boolean isOptional;
    private String optionalPrefix;
    private String optionalSuffix;
    private ChannelNumberFormat format;

    public ChannelHandler(String prefix, String suffix, boolean isOptional, String optionalPrefix,
                          String optionalSuffix, ChannelNumberFormat format) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.isOptional = isOptional;
        this.optionalPrefix = optionalPrefix;
        this.optionalSuffix = optionalSuffix;
        this.format = format;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public String serialize(int number, boolean onlyChannel) {
        if (isOptional && number == 1 && onlyChannel) {
            return prefix + suffix;
        } else {
            return prefix + optionalPrefix + format.getSerializer().serialize(number) + optionalSuffix + suffix;
        }
    }

    public int deserialize(String name) {
        if (isOptional && name.equals(prefix + suffix)) {
            return 1;
        } else {
            if (name.startsWith(prefix + optionalPrefix) && name.endsWith(optionalSuffix + suffix)) {
                String number = name.substring((prefix + optionalPrefix).length(),
                        name.length() - (optionalSuffix + suffix).length());

                return format.getDeserializer().deserialize(number);
            } else {
                return -1;
            }
        }
    }

    public static ChannelHandler fromConfigString(String name) {
        String prefix = null;
        String suffix = null;

        if (name.contains("%")) {
            String[] split = name.split("%", 3);

            name = split[1];

            if (split.length != 3) {
                throw new IOError(new IOException("Could parse channel configuration: " + name));
            }

            prefix = split[0];
            suffix = split[2];
        }

        ChannelNumberFormat numberFormat = null;

        for (ChannelNumberFormat value : ChannelNumberFormat.values()) {
            if (name.contains(value.getIdentifier())) {
                numberFormat = value;
            }
        }

        if (numberFormat == null) {
            throw new IOError(new IOException("No number format specified: " + name));
        }

        String[] split = name.split(Pattern.quote(numberFormat.getIdentifier()), 2);

        if (split.length != 2) {
            throw new IOError(new IOException("Multiple number formats specified: " + name));
        }

        if (prefix != null) {
            return new ChannelHandler(prefix, suffix, true, split[0], split[1], numberFormat);
        } else {
            return new ChannelHandler(split[0], split[1], false,
                    "", "", numberFormat);
        }
    }
}
