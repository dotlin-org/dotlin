extension ComparableOperators<T> on Comparable<T> {
  bool operator <(T other) => this.compareTo(other) < 0;

  bool operator >(T other) => this.compareTo(other) > 0;

  bool operator <=(T other) => this.compareTo(other) <= 0;

  bool operator >=(T other) => this.compareTo(other) >= 0;
}