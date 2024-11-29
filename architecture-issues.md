# Unresolved architecture issues

* Domain objects have jakarta.validation annotations, but they are not checked automatically.
  It would be better to have the builder (or constructor) automatically check those annotations
  to avoid invalid objects. Ideally, lombok would do that but it is still unsupported. There are
  long-standing issues on the lombok issue tracker.

