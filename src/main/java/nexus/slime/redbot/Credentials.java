package nexus.slime.redbot;

public record Credentials(
        String hostname,
        int port,
        String username,
        String password,
        int virtualServer
) {
    private static String getEnvironmentVariable(String name) {
        String value = System.getenv(name);

        if (value == null) {
            throw new RuntimeException("Missing environment variable: " + name);
        }

        return value;
    }

    public static Credentials fromEnvironment() {
        return new Credentials(
                getEnvironmentVariable("QUERY_HOSTNAME"),
                Integer.parseInt(getEnvironmentVariable("QUERY_PORT")),
                getEnvironmentVariable("QUERY_USERNAME"),
                getEnvironmentVariable("QUERY_PASSWORD"),
                Integer.parseInt(getEnvironmentVariable("VIRTUAL_SERVER"))
        );
    }
}
