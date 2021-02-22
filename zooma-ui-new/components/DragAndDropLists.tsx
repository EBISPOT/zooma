


/// Adapted from https://codesandbox.io/s/ql08j35j3q
/// Source: https://github.com/atlassian/react-beautiful-dnd/blob/master/docs/about/examples.md


import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';

const grid = 8;

const getItemStyle = (isDragging, draggableStyle) => ({
    // some basic styles to make the items look a bit nicer
    userSelect: 'none',
    padding: grid,
    margin: `0 0 ${grid}px 0`,

    // change background colour if dragging
    background: isDragging ? '#ccc' : '#ddd',

    // styles we need to apply on draggables
    ...draggableStyle
});

const getListStyle = isDraggingOver => ({
    background: isDraggingOver ? '#ccffcc' : 'white',
    padding: grid,
    width: 250,
    height: '300px',
    overflowY: 'scroll' as any,
    overflowX: 'none' as any
});

export interface ListEntry {
    id:string
    content:string
}

export interface List {
    title:string
    entries:ListEntry[]
}

interface Props {
    lists:List[]
    onChange:(lists:List[])=>void
}

interface State {
}


export default class DragAndDropLists extends Component<Props, State> {

    onDragEnd = result => {
        const { source, destination } = result;

        // dropped outside the list
        if (!destination) {
            return;
        }

        let listsCopy:List[] = this.props.lists.map(l => ({ title: l.title, entries: l.entries.slice(0) }))

        console.log('source ' + source.index + ' dest ' + destination.index)

        let sourceId = parseInt(source.droppableId.split('-')[1])
        let destId = parseInt(destination.droppableId.split('-')[1])

        if (sourceId === destId) {

            let list = listsCopy[sourceId]
            list.entries.splice(destination.index, 0, list.entries.splice(source.index, 1)[0]);

        } else {

            let sourceList = listsCopy[sourceId]
            let destList = listsCopy[destId]

            destList.entries.splice(destination.index, 0, sourceList.entries.splice(source.index, 1)[0]);
        }

        this.props.onChange(listsCopy);
    };

    render() {
        return (
            <DragDropContext onDragEnd={this.onDragEnd}>
                {
                    this.props.lists.map((list, i) => 
                        <Droppable droppableId={'droppable-' + i}>
                        {(provided, snapshot) => (
                            <div style={{display: 'inline-block', verticalAlign: 'top'}}>
                                <h4>{list.title}</h4>
                            <div
                                ref={provided.innerRef}
                                style={getListStyle(snapshot.isDraggingOver)}>
                                {list.entries.map((item, index) => {
                                    return <Draggable
                                        key={item.id}
                                        draggableId={item.id}
                                        index={index}>
                                        {(provided, snapshot) => (
                                            <div
                                                ref={provided.innerRef}
                                                {...provided.draggableProps}
                                                {...provided.dragHandleProps}
                                                style={getItemStyle(
                                                    snapshot.isDragging,
                                                    provided.draggableProps.style
                                                )}>
                                                {item.content}
                                            </div>
                                        )}
                                    </Draggable>
                                })}
                                {provided.placeholder}
                            </div>
                            </div>
                        )}
                    </Droppable>
                    )
                }
            </DragDropContext>
        );
    }
}
