# Introduction #

The DHCP Protocol is not very explicit in the exact implementation of DHCP Clients when receiving DHCP Offer messages from DHCP Servers. The protocol does not make clear how the DHCPClient should choose between offers. This attack method assumes that the client merely replies to the first available offer, and thus tries to be the first.

# Details #

As soon as a DHCP Discover message is received, instead of processing the request like a normal full fledged DHCP Server should, the rouge dhcp server should immediately broadcast a pre-calculated offer message, using the same xid (transaction id) of the discoverying client.

# Results #



# Analysis #

# Conclustion #

# Variations #
  * Unicast the reply rather than broadcast potentially quicker
  * Offer the client the same details as a dhcp server already on the network
    * taking this further, rouge server can masquerade as server on network, by presending its own dhcp discover message, thus gathering the servers mac address and ip. the rouge serve can now spoof the mac and ip address of the actual server, thus fooling the client into thinking that this is the server its used to configuring with.