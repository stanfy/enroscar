An example that shows how Goro can be combined with [Square's Tape](https://github.com/square/tape).
`SampleActivity` contains a button that creates a new task that is written to tape.
User is notified about scheduled operation as soon as the task is serialized.
Sample task tries to perform a network operation. If it fails, tape processing is stopped.
Connectivity change receiver revives tasks processing when connection is up.
Both writing tasks and their processing is scheduled with GoroService.
This example also demonstrates how Goro instance can be injected with Dagger.
