# Bibliotecas Distribuidas
Proyecto de Sistemas Distribuidos 2023-01

## 🔩 Uso
### Client
```sh
usage: Client [-h] -p PORT -a ADDRESS [-t TIMEOUT] [-r RETRIES] -f FILE

named arguments:
  -h, --help                    show this help message and exit
  -p PORT, --port PORT          RequestManager TCP Port
  -a ADDRESS, --address ADDRESS RequestManager ip address
  -t TIMEOUT, --timeout TIMEOUT Timeout in milliseconds (5000 by default)
  -r RETRIES, --retries RETRIES Number of retries (5 by default)
  -f FILE, --file FILE          Input file path
```

### LoadManager
```sh
usage: LoadManager [-h] -p PORT [-c CLIENTS]

named arguments:
-h, --help                      show this help message and exit
-p PORT, --port PORT            Connection TCP Port
-c CLIENTS, --clients CLIENTS   Number of concurrent clients (10 by default)
```

## 👷 Integrantes
* Abril Cano
* Angel Talero
* Juan Durán
* Juan Robledo