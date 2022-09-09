import * as React from 'react';

import { StyleSheet, View, Text, Pressable } from 'react-native';
import { init, createOrderToken } from 'react-native-pine-labs';

init("YOUR_ID", "YOUR_ID", "YOUR_ID")
export default function App() {

  const [sdkinit, setSdkinit] = React.useState<boolean>(false)
 /*  React.useEffect(() => {
    if (init) {
      init("2207751CCF0C41519E4082F9FFC68DDE", "12022", "708b0b81-50ea-4821-924b-61fcf1252c7a").then(info => {
        const { success } = info
        console.log("🚀 ~ file: App.tsx ~ line 14 ~ init ~ info", info)
        setSdkinit(success) 
      })
    }

  }, []) */

  const jsObject = {
    merchant_data: {
      merchant_id: 'YOUR_ID',
      merchant_access_code: '708b0b81-50ea-4821-924b-61fcf1252c7a',
      merchant_return_url: 'http://192.168.101.205:9073/chargingrespnew.aspx',
      merchant_order_id: `${new Date().getTime()}`,
    },
    payment_info_data: {
      amount: 200,
      currency_code: 'INR',
      order_desc: 'Test Order',
    },
    customer_data: {
      country_code: '91',
      mobile_number: '9121004028',
      email_id: 'john.doe@gmail.com',

    },
    billing_address_data: {
      first_name: 'John',
      last_name: 'Doe',
      address1: 'House No. 123',
      address2: 'Road XYZ',
      address3: 'Bengaluru',
      pin_code: '111111',
      city: 'Bengaluru',
      state: 'Karnataka',
      country: "India",
    },
    shipping_address_data: {
      first_name: 'John',
      last_name: 'Doe',
      address1: 'House No. 123',
      address2: 'Road XYZ',
      address3: 'Bengaluru',
      pin_code: '111111',
      city: 'Bengaluru',
      state: 'Karnataka',
      country: "India",
    },
    product_info_data: {
      product_details: [
        {
          product_code: '40',
          product_amount: 200,
        },
      ],
    },
    additional_info_data: {
      rfu1: '123',
    },
  }
  const [result, setResult] = React.useState<undefined>();


  const onPressFunction = async () => {
    const result = await createOrderToken(jsObject)
    setResult(result)
    console.log("createOrderToken", result);

  }
  return (
    <View style={styles.container}>

      <Text>{`sdk init : ${sdkinit} `}</Text>
      <Pressable style={{ padding: 10, backgroundColor: 'blue', marginVertical:50 }} onPress={onPressFunction}>
        <Text>I'm pressable!</Text>
      </Pressable>

      {result && <Text>{`result : ${JSON.stringify(result)}`}</Text>}





    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
