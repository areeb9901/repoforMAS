Request to modify the NAS mount configuration.

Currently, the NAS mount was ordered together for the following four servers:

- eurvlii108108.xmp
- eurvlii108425.xmp
- eurvlii06363.xmp
- eurvlii07391.xmp

The requirement is to keep the mount only on:

- eurvlii108108.xmp
- eurvlii108425.xmp

and remove the mount from:

- eurvlii06363.xmp (UAT1 Node1)
- eurvlii07391.xmp (UAT2 Node2)

As the NAS was requested for all four servers together, any change performed on one server is currently reflecting across all four servers. Therefore, please remove the mounting from the UAT nodes (eurvlii06363.xmp and eurvlii07391.xmp) and retain it only on the required servers.