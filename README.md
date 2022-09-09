# react-native-pine-labs
React native pinelabs sdk android
IOS Support coming soon 
## Installation

```sh
npm install react-native-pine-labs
```

## Usage

```js
import { init, createOrderToken } from 'react-native-pine-labs';

// ... you must intilise SDK before using it
init(your_merchantSecret, your_merchantId,   your_merchantAccessCode)

// ... below function will actully open Payment processor
const result = await createOrderToken(yourJsonObject);
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---
Follow on youtube [amusoftech](https://www.youtube.com/channel/UCtyZ9z0ulUQsyP-wLrNyJ4A) 
Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
