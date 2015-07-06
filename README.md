
## How to use?

```bash
sbt assembly
./run-on-yarn.sh
```

## How to check the job on Yarn

From command line it's possible to display the log:
```bash
yarn logs -applicationId <application ID>
```

From the Yarn UI, browse:
http://<yarn node manager host>:8088/


## How to kill the job

```
yarn application -kill <application ID>
```
