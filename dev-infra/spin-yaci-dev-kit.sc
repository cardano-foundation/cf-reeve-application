import $ivy.`com.bloxbean.cardano:cardano-client-lib:0.5.1`

import $ivy.`com.lihaoyi:requests_3:0.8.0`

//import com.bloxbean.cardano.client.api.model.Amount
// import com.bloxbean.cardano.client.crypto._
 import com.bloxbean.cardano.client.account._
// import com.bloxbean.cardano.client.common.model._

import com.bloxbean.cardano.client.common.model.Networks

import com.bloxbean.cardano.client.address._

// import com.bloxbean.cardano.client.crypto.Blake2bUtil._
// import com.bloxbean.cardano.client.common.cbor._
// import com.bloxbean.cardano.client.metadata._

// import com.bloxbean.cardano.client.common._

// import java.math._
// import java.util.Random

val ownerMnemonics = "ocean sad mixture disease faith once celery mind clay hidden brush brown you sponsor dawn good claim gloom market world online twist laptop thrive"

val account = new Account(Networks.testnet(), ownerMnemonics)

def topUpAccount(newAcc: Account, amount: Int): Boolean = {
  val jsonTopUpPayload = s""" {
        "address": \"${newAcc.baseAddress()}\",
        "adaAmount": $amount
    }
    """.stripMargin

  val r = requests.post(
    "http://localhost:10000/local-cluster/api/addresses/topup",
    headers = Map("Content-Type" -> "application/json"),
    data = jsonTopUpPayload
  )

  var res = if (r.statusCode == 200) {
    println(s"Topup with ${amount} for addr:${newAcc.baseAddress} success!")

    true
  } else {
    println(s"Topup with ${amount} for addr:${newAcc.baseAddress} failed!")

    false
  }

  println("Sleeping for 1 sec")
  Thread.sleep(1000)

  res
}

@main
def main() = {
  topUpAccount(account, 10000)
}