#An overview of the dhcp protocol

# Introduction #

The DHCP or Dynamic Host Configuration Protocol, is a networking protocol that dynamically allocates unique ip addresss (from a pool of ip's) and network configurations to hosts on a network.

# Details #
The typical DHCP Server/Client interaction is as follows:

**DHCP Clients** (Hosts requiring configuration details) broadcast a **DHCP Discover Message** on the network to determine if any DHCP Servers are present.

**DHCP Servers** present on the network reply with a **DHCP Offer Message** offering the requesting client network configuration data, including an IP Address.

Upon accepting a particiar DHCP Server's offer, the **DHCP Client** sends a DHCP Request Message explicitly requesting the use of network configuration details from a DHCP Server.

The DHCP Server replies to the DHCP Request with a DHCP ACK Message, to notify the DHCP Client that the client's use of the server's offered network configuration has been acknnowledged, and that their requested IP address has been assigned.