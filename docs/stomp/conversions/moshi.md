# STOMP with Moshi

The `krossbow-stomp-moshi` module is a JVM-only extension of `krossbow-stomp-core` that provides new APIs to
send and receive properly typed classes, and automatically convert them to/from the JSON bodies of STOMP frames
by leveraging [Moshi](https://github.com/square/moshi).

The main addition is the extension function `StompSession.withMoshi()`, which turns your `StompSession`
into a `TypedStompSession`.
This new session type has additional methods that use Moshi to convert your objects into JSON and back:

```kotlin
// example Moshi instance that converts Kotlin types using reflection
val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()
StompClient(WebSocketClient.builtIn()).connect(url).withMoshi(moshi).use { session ->
    session.convertAndSend("/some/destination", Person("Bob", 42)) 

    val messages: Flow<MyMessage> = session.subscribe<MyMessage>("/some/topic/destination")
    val firstMessage: MyMessage = messages.first()

    println("Received: $firstMessage")
}
```

## Dependency

To use Moshi conversions, add `krossbow-stomp-moshi` to your Gradle dependencies
(`krossbow-stomp-core` is unnecessary because it's transitively brought by this one):

```kotlin
implementation("org.hildan.krossbow:krossbow-stomp-moshi:{{ git.short_tag }}")
```

This dependency transitively brings Moshi {{ versions.moshi }}.
