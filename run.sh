#!/usr/bin/env bash

nodes(){
  local COMMAND_NAME="$1"

  case "$COMMAND_NAME" in
    "start")
      ./gradlew clean deployNodes
      ./build/nodes/runnodes
      ;;
    "stop")
      local NODE_PIDS=$(pgrep -f net.corda.node.Corda)

      if [[ -n "$NODE_PIDS" ]]; then
        kill $NODE_PIDS
      fi
      ;;
    *)
      echo "Unknown network command '$COMMAND_NAME'"
      exit 1
      ;;
  esac
}

shell(){
  local NODE_NAME="$1"

  case "$NODE_NAME" in
      "mint")
        ssh user1@localhost -p 3333
        ;;
      "tradera")
        ssh user1@localhost -p 4444
        ;;
      "traderb")
        ssh user1@localhost -p 5555
        ;;
      *)
        echo "Unknown node '$NODE_NAME'"
        exit 1
        ;;
    esac
}

usage(){
  cat <<EOF
$0 nodes <start|stop>              Starts or stops the corda nodes
$0 shell <mint|tradera|traderb>    Connects to the given corda node's shell via SSH

When connecting via SSH you can execute the following commands on the corda node's shell to test the metals-cordapp:

corda>>> flow start IssueMetalFlow material: Gold, weight: 10, owner: "O=TraderA,L=New York,C=US"        # nodes: mint
corda>>> run vaultQuery contractStateType: com.acmecorp.states.MetalState                                # nodes: mint, tradera
corda>>> flow start TransferMetalFlow material: Gold, weight: 10, newOwner: "O=TraderB,L=New York,C=US"  # nodes: tradera
corda>>> run vaultQuery contractStateType: com.acmecorp.states.MetalState                                # nodes: mint, tradera, traderb
EOF
}

[[ $# == 0 ]] && usage
"$@"