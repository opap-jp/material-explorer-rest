export default class Dictionaries {
    public static toDictionary = function<T, K>(items: T[], keySelector: (entry: T) => string): { [key: string] : T } {
        let dictionary: { [key: string] : T } = {};
        for (var i = 0; i < items.length; i++) {
            let item = items[i];
            dictionary[keySelector(item)] = item;
        }
        return dictionary;
    }    
}
