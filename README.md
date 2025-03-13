# Exercise 4: Operating on Linked Data

Sandro

This repository contains a partial implementation of a [JaCaMo](https://jacamo-lang.github.io/) application, in which autonomous agents are able to operate on Linked Data in a distributed social network — and to manage personal data of a user.

## Table of Contents
- [Project structure](#project-structure)
- [Task 1](#task-1)
  - [Update your FOAF Profile](#update-your-foaf-profile)
  - [Query the distributed social graph](#query-the-distributed-social-graph)
- [Task 2](#task-2)
  - [Edit the Access Control List of your Solid pod](#edit-the-access-control-list-of-your-solid-pod)
  - [Interact with a Linked Data Platform Container](#interact-with-a-linked-data-platform-container)
    - [Creating an LDP container](#1-creating-an-ldp-container)
    - [Adding data to an LDP container](#2-adding-data-to-an-ldp-container)
    - [Reading data from an LDP container](#3-reading-data-from-an-ldp-container)
- [How to run the project](#how-to-run-the-project)


## Project structure
```
├── src
│   ├── agt
│   │   ├── movie_manager.asl // agent program of the agent that stores in your pod the titles of movies you add to your watchlist
│   │   ├── wristband_manager.asl // agent program of the agent that stores in your pod information about your sleep and running trails
│   │   └── personal_assistant.asl // agent program of the agent that recommends to you activities based on your personal data
│   └── env
│       └── solid
│           └── Pod.java // artifact that can be used for interacting with an LDP container in a Solid pod
└── task.jcm // the configuration file of the JaCaMo application
```

## Task 1

### Update your FOAF profile

Go to [https://wiser-solid-xi.interactions.ics.unisg.ch/.account/login/password/register/](https://wiser-solid-xi.interactions.ics.unisg.ch/.account/login/password/register/)and create a Solid Pod.

You get a WebId for this pod that you should edit its associated profile, using the [foaf](http://xmlns.com/foaf/spec/) ontology with:
- your name
- your email
- the WebId of the people that you know from the group

WARNING: If you edit your WebID profile with a file that is not a proper Turtle document, you will lose access to your Pod. You can use a Turtle editor (e.g., https://ci.mines-stetienne.fr/teaching/semweb/turtle.html) to edit your profile and ensure that it satisfies the right Turtle syntax. If you have an issue, you should contact Jérémy and Alessandro for reinitializing the profile. To do so, please provide the last valid version of the profile.

Use a Pod editor, such as [Penny](https://penny.vincenttunru.com/) to edit your Pod.

### Query the distributed social graph

Install Comunica with:

```bash
npm install -g @comunica/query-sparql
```

1. Perform a comunica-sparql query to get the WebIds of the people you know.

Install Comunica for Link Traversal with:

```bash
npm install -g @comunica/query-sparql-link-traversal
```

2. Perform a comunica-sparql-link-traversal query to get all the names of the people you know directly or indirectly. Compare with the result of the request performed without link traversal.

## Task 2
### Edit the Access Control List of your Solid pod
- Use [Penny](https://penny.vincenttunru.com/) to navigate and edit the Access Control List (ACL) of your Solid pod (https://wiser-solid-xi.interactions.ics.unisg.ch/your-pod-name/.acl). Update the `<#public>` authorization rule based on the [Web Access Control vocabulary](https://solid.github.io/web-access-control-spec/#authorization-rule) so that agents can interact with your pod to read and/or write data:
```
@prefix acl: <http://www.w3.org/ns/auth/acl#>.
@prefix foaf: <http://xmlns.com/foaf/0.1/>.

<#public>
    a acl:Authorization;
    acl:agentClass foaf:Agent;
    acl:accessTo <./>;
    acl:mode acl:Read, acl:Write, acl:Control;
    acl:default <./>.
```

Using [Penny](https://penny.vincenttunru.com/), setting the permissions can also be done without directly writing the Turtle file representing the ACL. This option is safer.

### Interact with a Linked Data Platform Container
First, update line 24 in the JaCaMo configuration file [task.jcm](task.jcm), so that the project uses your own Solid pod:
```
artifact pod: solid.Pod("https://wiser-solid-xi.interactions.ics.unisg.ch/your-pod-name")
```

Second, complete the tasks that follow to enable autonomous agents to interact with [our group's Solid Community Server](https://wiser-solid-xi.interactions.ics.unisg.ch) that implements the [Solid protocol](https://solidproject.org/TR/protocol), based on the [W3C Linked Data Platform (LDP) specification](https://www.w3.org/TR/ldp/). The API of our group’s Solid Community Server is [documented here](https://communitysolidserver.github.io/CommunitySolidServer/latest/).

With the completion of the tasks you should expect the following project behavior: The personal assistant ([personal_assistant.asl](src/agt/personal_assistant.asl)) creates an LDP container in your pod, named "personal-data", and informs the other agents that a new container is available. As a result, the movie manager ([movie_manager.asl](src/agt/movie_manager.asl)) stores in the container the titles of movies you add to your watchlist (e.g. Matrix, Inseption etc.) within a file watchlist.txt. The wristband manager ([wristband_manager.asl](src/agt/wristband_manager.asl)) stores in the container information about the number of hours you sleep every night (e.g. 6h, 7h, 5h etc. every 4000ms) within a file sleep.txt. Additionally, the wristband manager stores in the container information about the distance of the running trails that you follow every time you train (e.g. 3km, 5.5km, 5.5km etc. every 4000s) within a file trails.txt. Every 10000ms, the personal assistant reads the data available in the "personal-data" container to make movie or running trail suggestions based on the number of hours you slept last night.

#### 1. Creating an LDP container
Implement the method `createContainer()` of the Java class [`Pod`](src/env/solid/Pod.java) that enables agents to to create an LDP container in your pod. You can check whether a container is already present in order not to create it again, which would cause additional containers that you do not need to be created.
- TIP: [Creating containers based on the Solid Community Server documentation](https://communitysolidserver.github.io/CommunitySolidServer/latest/usage/example-requests/#put-creating-resources-for-a-given-url)
- TIP: You should create a container as an empty resource, whose URL ends with "/".
- TIP: You can optionally check that your container is not present before creating the container.

#### 2. Adding data to an LDP container
Implement the method `publishData()` of the Java class [`Pod`](src/env/solid/Pod.java) that enables agents to publish data (text/plain) to an LDP container in your pod.
- TIP: [Creating resources (e.g. publishing data to a container) based on the Solid Community Server documentation](https://communitysolidserver.github.io/CommunitySolidServer/latest/usage/example-requests/#put-creating-resources-for-a-given-url)
- TIP: use the provided method: createStringFromArray in [`Pod`](src/env/solid/Pod.java).

#### 3. Reading data from an LDP container
Implement the method `readData()` of the Java class [`Pod`](src/env/solid/Pod.java) that enables agents to read data (text/plain) from an LDP container in your pod.
- TIP: [Retrieving resources (e.g. reading data from a container) based on the Solid Community Server documentation](https://communitysolidserver.github.io/CommunitySolidServer/latest/usage/example-requests/#get-retrieving-resources)
- TIP: use the provided method: createArrayFromString in [`Pod`](src/env/solid/Pod.java).

## How to run the project
You can run the project directly in Visual Studio Code or from the command line with Gradle 7.4.
- In VSCode:  Click on the Gradle Side Bar elephant icon, and navigate through `GRADLE PROJECTS` > `exercise-4` > `Tasks` > `jacamo` > `task`.
- On MacOS and Linux run the following command:
```shell
./gradlew task
```
- On Windows run the following command:
```shell
gradle.bat task
```
- On Intellij IDEA, you can run one of the command above (depending on your OS), using the "▶" symbol that appears on your IDE.
