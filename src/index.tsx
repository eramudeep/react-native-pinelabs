import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-pine-labs' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const PineLabs = NativeModules.PineLabs  ? NativeModules.PineLabs  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function init( merchantSecret:string ,   merchantId:string,   merchantAccessCode:string ): Promise<boolean> {  
  return PineLabs.initSDK(merchantSecret,merchantId,merchantAccessCode );
}  
export function createOrderToken(jsonRequest: object ): Promise<number> {
      return PineLabs.createOrderToken(JSON.stringify(jsonRequest, null, 1) );
}
 
