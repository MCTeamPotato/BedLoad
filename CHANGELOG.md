# 1.2.0
- Fixed a bug where removing a single chunk loader block (e.g., a bed) would unload the entire chunk even if other loader blocks were still present.

- Added persistent tracking of chunk loader blocks per chunk using ForceLoadReasons (SavedData), ensuring chunks remain loaded as long as at least one loader block exists.