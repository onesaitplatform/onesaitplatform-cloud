import Components from '../Components';
import HTMLTableEditDisplay from './editForm/HTMLtable.edit.display';
import HTMLTableEditLogic from './editForm/HTMLtable.edit.logic';

export default function(...extend) {
  return Components.baseEditForm([
    {
      key: 'display',
      components: HTMLTableEditDisplay,
    },
    {
      key: 'data',
      ignore: true,
    },
    {
      key: 'validation',
      ignore: true,
    },
    {
      key: 'logic',
      components: HTMLTableEditLogic,
    },
    {
      key: 'addons',
      ignore: true
    },
  ], ...extend);
}
