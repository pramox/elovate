export interface ValuePair {
  name: Date;
  value: number;
}

export interface LinePlotContent {
  name: string;
  series: ValuePair[];
}

export interface LinePlotData extends Array<LinePlotContent>{}
