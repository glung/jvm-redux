# jvm-redux

Avoiding duplicate efforts. 

The project defines an API & Specs to implement [Redux](http://redux.js.org/) on the JVM. 
It provides adapters for some existing implementations. 


## Context 

A simple [Google request ](https://www.google.de/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=redux%20kotlin) reveals many implementations of [Redux](http://redux.js.org/) for the JVM. 

This project has two goals :
- Evaluate a given implementation (compliance to specs)
- Provides a high-level API. Hence, any library can be written once and fit then all.


## Api

Redux is better expressed using a functionnal language, hence the API is defined in [Kotlin](https://kotlinlang.org/). The implementations may use another language.

Please, find the API [here](https://github.com/glung/jvm-redux/tree/master/specs/src/main/kotlin/redux/api)

The repository also provides adapters for the following implementations :
- [Bansa](https://github.com/brianegan/bansa)
- [Kedux](https://github.com/AngusMorton/kedux)
- [Redux-Kotlin](https://github.com/pardom/redux-kotlin/)
- [Reduks](https://github.com/beyondeye/Reduks)
- [Redux-Java](https://github.com/glung/redux-java)

Please, create an issue if you want to add an adapter to another implementation. 

A comparison of stores is available [here](Comparisons.md)

**Acknowledgements** : The api started out of [Redux-Kotlin](https://github.com/pardom/redux-kotlin/) and some updates were made to it. Redux-Kotlin has also a nice port of the official Redux project, for that reason the tests were also imported. Thank you @pardom.

## Projects using this API

Any add-ons (Middlewares, DevTools, ...) written on top of this API can fit any implementation with an adapter. 

Examples : 
- [jvm-redux-devtools-instrument](https://github.com/glung/jvm-redux-devtools-instrument) : dev tools

## Import project

### Repository

Set-up [JitPack](https://jitpack.io)

```
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```

### API 

`compile 'com.github.glung.jvm-redux:specs:-SNAPSHOT'`

### Adapter for Bansa

`compile 'com.github.glung.jvm-redux:adapter-bansa:-SNAPSHOT'`


### Adapter for Kedux

`compile 'com.github.glung.jvm-redux:adapter-kedux:-SNAPSHOT'`


### Adapter for Reduks

`compile 'com.github.glung.jvm-redux:adapter-reduks:-SNAPSHOT'`


### Adapter for Redux-Java
    
`compile 'com.github.glung.jvm-redux:adapter-redux-java:-SNAPSHOT'`

    
### Adapter for Redux-Kotlin
    
`compile 'com.github.glung.jvm-redux:adapter-redux-kotlin:-SNAPSHOT'`
