# TenIO Examples

Runnable Java 21 examples for `tenio-core:1.0.0` and `tenio-engine:1.0.0`.

## Build and run

Install the updated core locally before building examples if version 1.0.0 has not yet been published:

```sh
# In tenio-core
mvn -Dgpg.skip=true install

# In tenio-examples
mvn test package
```

The assembly defaults to example1's login server. Run it from the examples directory:

```sh
java -jar target/tenio-examples-1.0.0-jar-with-dependencies.jar configuration.example1.xml
```

For another entry point, use the assembled JAR as the classpath:

```sh
java -cp target/tenio-examples-1.0.0-jar-with-dependencies.jar com.tenio.examples.example3.TestServerAccessDatagramChannel configuration.example3.xml
java -cp target/tenio-examples-1.0.0-jar-with-dependencies.jar com.tenio.examples.example3.TestClientAccessDatagramChannel
```

Start each server before its client. Each example uses its own package as its component scan root. Start separate examples in separate JVMs; the server runtime still contains global services. TCP uses port 8032 unless its configuration says otherwise. Clients currently target localhost; the shared transports also accept explicit remote hosts.

## Simple Implementation
- Establishes a simple server with only a single Java class

```Java
/**
 * This class shows how a simple server handle messages that come from a client.
 */
@Bootstrap
@EventHandler
public final class SimpleServer extends AbstractHandler implements EventConnectionEstablishedResult<ZeroMap>,
        EventPlayerLogin<Player>, EventReceivedMessageFromPlayer<Player, DataCollection> {

    static void main() {
        ApplicationLauncher.run(SimpleServer.class);
    }

    @Override
    public void onConnectionEstablishedResult(Session session, ZeroMap message, ConnectionEstablishedResult result) {
        if (result == ConnectionEstablishedResult.SUCCESS) {
            api().login(message.getString(SharedEventKey.KEY_PLAYER_LOGIN), session);
        }
    }

    @Override
    public void onPlayerLogin(Player player) {
        var parcel = map().putString(SharedEventKey.KEY_PLAYER_LOGIN,
                String.format("Welcome to server: %s", player.getIdentity()));

        response().setContent(parcel).setRecipientPlayer(player).write();
    }

    @Override
    public void onReceivedMessageFromPlayer(Player player, DataCollection message) {
        DataCollection parcel = null;
        if (message instanceof ZeroMap request) {
            parcel = map().putString(SharedEventKey.KEY_CLIENT_SERVER_ECHO,
                    String.format("Echo(%s): %s", player.getIdentity(),
                            request.getString(SharedEventKey.KEY_CLIENT_SERVER_ECHO)));
        } else if (message instanceof MsgPackMap request) {
            parcel = msgmap().putString(SharedEventKey.KEY_CLIENT_SERVER_ECHO,
                    String.format("Echo(%s): %s", player.getIdentity(),
                            request.getString(SharedEventKey.KEY_CLIENT_SERVER_ECHO)));
        }

        response().setContent(parcel).setRecipientPlayer(player).write();
    }
}
```

## Examples

| Package | Demonstrates | Configuration |
| --- | --- | --- |
| example0 | Minimal server and mixed ZeroMap/MsgPack echoes | Embedded default; accepts an XML path |
| example1 | Login, request policy, custom codec extension points | configuration.example1.xml |
| example2 | Finite state machine and message dispatch | No network server |
| example3 | TCP login, UDP registration, convey ID, ZeroMap echo | configuration.example3.xml |
| example4 | Movement simulation, steering, UDP statistics | configuration.example4.xml |
| example5 | Entity/component/system simulation | No network server |
| example6 | TCP echo load generator | configuration.example6.xml |
| example7 | WebSocket server and room handlers | configuration.example7.xml |
| example8 | HTTP server and Java HttpClient request | configuration.example8.xml |
| example10 | TCP login and MsgPack UDP registration/echo | configuration.example10.xml |
| example11 | Custom terminal commands | configuration.example11.xml |
| example12 | Profile-selected components, factory methods, fields and event handlers | Three configurations below |

Example6 defaults to 100 clients. Use `-Dtenio.clients=10000` before the main class to opt into a larger load test. It uses one platform reader thread per client and is a demonstration, not a high-scale benchmark driver.

## Example 11: Desktop Monitoring and Custom Commands

Run the server with `configuration.example11.xml`. It enables `monitor` and `history`.
On a machine with a graphical desktop, TenIO opens the monitor window. Enter `help`
to see the available commands. Try `player logout first` after connecting a test
client, then run `history` to see the disconnect. Use Up and Down to recall commands.

The original echo messages still work. Optional ZeroMap commands use short `op=1`
and string `name` to create a room, or short `op=2` and string `chat` to send a
message to the current room. Room creation is limited to 1 request per second with a
burst of 2, and chat to 2 per second with a burst of 4, per player. Enter `limits` to
see accepted and rejected requests. TenIO also applies its normal session queue policies.

Remove `monitor` from the example profile to run without the window and system
commands. With `history` still present, TenIO writes a TSV file under `logs/history/`.
See the TenIO Core README for the full profile reference.

## Example 12: Profiles

Run the same server with different XML files:

```sh
java -cp target/tenio-examples-1.0.0-jar-with-dependencies.jar com.tenio.examples.example12.TestServerProfiles configuration.example12.development.xml
```

In another terminal:

```sh
java -cp target/tenio-examples-1.0.0-jar-with-dependencies.jar com.tenio.examples.example12.TestClientProfiles
```

Stop the server, then repeat with another configuration:

| XML file | Active profiles | Selected behavior |
| --- | --- | --- |
| configuration.example12.development.xml | development | Development greeting component and development Banner factory method |
| configuration.example12.production.xml | production | Production greeting component and production Banner factory method |
| configuration.example12.development-metrics.xml | development,metrics | Development behavior plus LoginMetrics, profile-gated field injection and MetricsHandler |

The production profile is an instructional configuration, not a hardened deployment template.

The corresponding source examples are:

- `DevelopmentGreetingService` / `ProductionGreetingService`: alternative `@Component(profile = "...")` implementations of `GreetingService`.
- `BannerFactory`: `@BeanFactory(profile = {"development", "production"})` and per-method `@Bean(profile = "...")` selection.
- `ProfileServices`: shared components can inject the selected implementation; `@Autowired(profile = "metrics")` activates a required field only for metrics.
- `MetricsHandler`: `@EventHandler(profile = "metrics")` adds optional CCU reporting.
- `TestServerProfiles`: `@Bootstrap(profile = {"development", "production"})` admits either base profile.

XML selects profiles under the top-level Server/Properties section:

```xml
<Property name="profile">development,metrics</Property>
```

Profile names are trimmed and case-sensitive. An annotation's listed names are OR alternatives. Empty annotation restrictions participate in every profile; they do not imply a profile named `default`. XML must supply at least one profile. Selection happens at startup and is not a runtime feature toggle. Profiles do not replace bean qualifiers.

Choose **one** of development or production in this example. Activating both introduces competing implementations and factory products. Using only metrics omits the base profile needed by the bootstrap and greeting service. Other examples explicitly select `default` and keep their unrestricted annotations.

## Shared Example Client

`client/TCP`, `client/UDP`, `ClientCodec` and `ClientTasks` centralize transport and task lifecycle behavior. New code should connect explicitly, assign its fields, configure error/disconnection handlers, then start receiving. Constructor callbacks remain only as compatibility adapters.

TCP sends are synchronous and serialized. Listener callbacks run on the reader and must remain short; the simple echo examples schedule delayed sends instead of sleeping in callbacks. Closing a transport stops its reader; close owned timers and UDP together when TCP disconnects. UDP decodes only the received byte range and filters unrelated senders in peer-specific mode.

The sample compression/encryption classes are **pass-through teaching placeholders**. They neither compress nor protect data and must match the server's configured codecs. UDP registration by player name demonstrates the flow; a deployed application needs authenticated registration. Configure TCP main-connection expiry only alongside a suitable application heartbeat. UDP activity cannot keep TCP alive.

## Requirements
Java 21+

## License
The [`TenIO`](https://github.com/congcoi123/tenio) project is currently available under the [MIT](LICENSE) License.

## Changelog
Please check out the [changelog](CHANGELOG.md) for more details.

## Contributing
Please check out the [checklist](CHECKLIST.md) and [contributing guideline](CONTRIBUTING.md) for more details.

## Installation
GitHub
```sh
$ git clone https://github.com/congcoi123/tenio-examples.git
```

> Happy coding !
