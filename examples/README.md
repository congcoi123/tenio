## Examples
Please start the server before its corresponding client in each example package.
```code
$ java <server_main_class> <server_configuration_file.xml>
```
For instance:
```code
$ java TestServerLogin configuration.example1.xml
```

```txt
|-- example
    |-- example1
    |   |-- TestClientLogin
    |   |-- TestServerLogin
    |-- example2
    |   |-- (*)TestFsmMechanism
    |-- example3
    |   |-- TestClientAttach
    |   |-- TestServerAttach
    |-- example4
    |   |-- TestClientMovement
    |   |-- TestServerMovement
    |   |-- (*)TestMovementMechanism
    |-- example5
    |   |-- (*)TestEcsMechanism
    |-- example6
    |   |-- TestClientEchoStress
    |   |-- TestServerEchoStress
    |-- example7
    |   |-- TestServerWebsocket
    |-- example8
    |   |-- TestClientRestful
    |   |-- TestServerRestful
```

> Happy coding !
