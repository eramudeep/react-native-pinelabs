# react-native-pine-labs
React native pinelabs sdk android 
## Installation

```sh
npm install react-native-pine-labs
```

## Usage

```js
import { init, createOrderToken } from 'react-native-pine-labs';

// ...
init(your_merchantSecret, your_merchantId,   your_merchantAccessCode)
const result = await createOrderToken(yourJsonObject);
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
